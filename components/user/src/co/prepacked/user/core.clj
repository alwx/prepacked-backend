(ns co.prepacked.user.core
  (:require 
    [buddy.sign.jwt]
    [crypto.password.pbkdf2 :as crypto]
    [java-time]
    [co.prepacked.env.interface-ns :as env]
    [co.prepacked.user.store :as store]))

(defn- token-secret []
  (if (contains? env/env :token-secret)
    (env/env :token-secret)
    "some-default-secret"))

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
  (if-let [user (store/find-by-email email)]
    (if (crypto/check password (:password user))
      (let [new-token (generate-token email (:username user))]
        [true (user->visible-user user new-token)])
      [false {:errors {:password ["Invalid password."]}}])
    [false {:errors {:email ["Invalid email."]}}]))

(defn register! [{:keys [username email password]}]
  (if-let [_ (store/find-by-email email)]
    [false {:errors {:email ["A user with the provided email already exists."]}}]
    (if-let [_ (store/find-by-username username)]
      [false {:errors {:username ["A user with the provided username already exists."]}}]
      (let [new-token (generate-token email username)
            user-input {:email    email
                        :username username
                        :password (encrypt-password password)}]
        (store/insert-user! user-input)
        (if-let [user (store/find-by-email email)]
          [true (user->visible-user user new-token)]
          [false {:errors {:other ["Cannot insert user into database."]}}])))))

(defn user-by-token [token]
  (try
    (let [opts {:now (-> 
                       (java-time/instant) 
                       (java-time/to-millis-from-epoch))}
          claims (buddy.sign.jwt/unsign token (token-secret) opts)
          username (:sub claims)
          user (store/find-by-username username)]
      (if user
        [true (user->visible-user user token)]
        [false {:errors {:token ["Cannot find a user with associated token."]}}]))
    (catch Exception _
      [false {:errors {:token ["Token has expired"]}}])))

(defn update-user! [auth-user {:keys [username email password]}]
  (if (and 
        (not (nil? email))
        (not= email (:email auth-user))
        (not (nil? (store/find-by-email email))))
    [false {:errors {:email ["A user with the provided email already exists."]}}]
    (if (and 
          (not (nil? username))
          (not= username (:username auth-user))
          (not (nil? (store/find-by-username username))))
      [false {:errors {:username ["A user with the provided username already exists."]}}]
      (let [email-to-use (if email email (:email auth-user))
            user-input (filter #(-> % val nil? not)
                         {:password (when password (encrypt-password password))
                          :email    (when email email)
                          :username (when username username)})]
        (store/update-user! (:id auth-user) user-input)
        (if-let [updated-user (store/find-by-email email-to-use)]
          [true (user->visible-user updated-user (:token auth-user))]
          [false {:errors {:other ["Cannot update the user."]}}])))))
