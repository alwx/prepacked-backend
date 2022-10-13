(ns co.prepacked.user.core
  (:require [buddy.sign.jwt]
            [crypto.password.pbkdf2 :as crypto]
            [java-time]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.env.interface-ns :as env]
            [co.prepacked.log.interface-ns :as log]
            [co.prepacked.user.store :as store]
            [clojure.java.jdbc :as jdbc]))

(defn token-secret []
  (or (env/get-var :token-secret)
      (do
        (log/warn "`:token-secret` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn- generate-token [email username]
  (let [now (java-time/instant)
        claim {:sub username
               :iss email
               :exp (-> now
                        (java-time/plus (java-time/days 7))
                        (java-time/to-millis-from-epoch))
               :iat (java-time/to-millis-from-epoch now)}]
    (buddy.sign.jwt/sign claim (token-secret))))

(defn encrypt-password [password]
  (-> password crypto/encrypt str))

(defn user->visible-user [user token]
  {:user (-> user
             (assoc :token token)
             (dissoc :password))})

(defn login! [{:keys [email password]}]
  (let [db (database/db)]
    (if-let [user (store/find-by-email db email)]
      (if (crypto/check password (:password user))
        (let [new-token (generate-token email (:username user))]
          [true (user->visible-user user new-token)])
        [false {:errors {:password ["Invalid password."]} :-code 400}])
      [false {:errors {:email ["Invalid email."]} :-code 400}])))

(defn register! [{:keys [username email password]}]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [_ (store/find-by-email con email)]
      [false {:errors {:email ["A user with the provided email already exists."]} :-code 400}]
      (if-let [_ (store/find-by-username con username)]
        [false {:errors {:username ["A user with the provided username already exists."]} :-code 400}]
        (let [new-token (generate-token email username)
              now (java-time/instant)
              user-input {:email    email
                          :username username
                          :password (encrypt-password password)
                          :created_at (database/instant->sql-timestamp now)
                          :updated_at (database/instant->sql-timestamp now)}]
          (store/insert-user! con user-input)
          (if-let [user (store/find-by-email con email)]
            [true (user->visible-user user new-token)]
            [false {:errors {:other ["Cannot insert user into database."]} :-code 500}]))))))

(defn user-by-token [token]
  (try
    (let [con (database/db)
          opts {:now (->
                      (java-time/instant)
                      (java-time/to-millis-from-epoch))}
          claims (buddy.sign.jwt/unsign token (token-secret) opts)
          username (:sub claims)
          user (store/find-by-username con username)]
      (if user
        [true (user->visible-user user token)]
        [false {:errors {:token ["Cannot find a user with associated token."]}}]))
    (catch Exception _
      [false {:errors {:token ["Token has expired"]}}])))

(defn update-user! [auth-user {:keys [username email password]}]
  (jdbc/with-db-transaction [con (database/db)]
    (if (and
         (not (nil? email))
         (not= email (:email auth-user))
         (not (nil? (store/find-by-email con email))))
      [false {:errors {:email ["A user with the provided email already exists."]} :-code 400}]
      (if (and
           (not (nil? username))
           (not= username (:username auth-user))
           (not (nil? (store/find-by-username con username))))
        [false {:errors {:username ["A user with the provided username already exists."]} :-code 400}]
        (let [email-to-use (if email email (:email auth-user))
              now (java-time/instant)
              user-input (filter #(-> % val nil? not)
                                 {:password (when password (encrypt-password password))
                                  :email    (when email email)
                                  :username (when username username)
                                  :updated_at (database/instant->sql-timestamp now)})]
          (store/update-user! con (:id auth-user) user-input)
          (if-let [updated-user (store/find-by-email con email-to-use)]
            [true (user->visible-user updated-user (:token auth-user))]
            [false {:errors {:other ["Cannot update the user."]} :-code 500}]))))))
