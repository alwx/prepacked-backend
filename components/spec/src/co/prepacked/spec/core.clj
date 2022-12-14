(ns co.prepacked.spec.core
  (:require 
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.string :as str]
    [spec-tools.core :as st])
  (:import 
    (java.util UUID)))

(def ^:private email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def ^:private uri-regex #"https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")
(def ^:private slug-regex #"^[a-z0-9]+(?:-[a-z0-9]+)*$")
(def ^:private uuid-regex #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(def non-empty-string?
  (st/spec {:spec (s/and string? #(not (str/blank? %)))
            :type :string
            :description "Non empty string spec. Checks with clojure.string/blank?"}))

(def username?
  (st/spec {:spec non-empty-string?
            :type :string
            :description "A non empty string spec with a special username (UUID) generator."
            :gen #(gen/fmap (fn [_] (str (UUID/randomUUID)))
                            (gen/string-alphanumeric))
            :swagger/example "non-empty-string"}))

(def email?
  (st/spec {:spec (s/and string? #(re-matches email-regex %))
            :type :string
            :description "A string spec that conforms to email regex." 
            :gen #(gen/fmap (fn [[s1 s2]] (str s1 "@" s2 ".com"))
                            (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))
            :swagger/example "email@alwxdev.com"}))

(def uri-string?
  (st/spec {:spec (s/and string? #(re-matches uri-regex %))
            :type :string
            :description "A string spec that conforms to URI regex."
            :gen #(gen/fmap (fn [[c1 c2]]
                                      (let [s1 (apply str c1)
                                            s2 (apply str c2)]
                                        (str "http://" s1 "." (subs s2 0 (if (< 3 (count s2)) 3 (count s2))))))
                            (gen/tuple 
                              (gen/vector (gen/char-alpha) 2 100) 
                              (gen/vector (gen/char-alpha) 2 5)))
            :swagger/example "https://alwxdev.com"}))

(def slug?
  (st/spec {:spec (s/and string? #(re-matches slug-regex %))
            :type :string
            :description "A string spec that conforms to slug regex."
            :gen #(gen/fmap (fn [[c1 c2]]
                              (let [s1 (str/lower-case (apply str c1))
                                    s2 (str/lower-case (apply str c2))]
                                (str s1 "-" s2)))
                            (gen/tuple
                             (gen/vector (gen/char-alpha) 2 10)
                             (gen/vector (gen/char-alpha) 2 10)))
            :swagger/example "some-slug"}))

(def password?
  (st/spec {:spec (s/and string? #(<= 8 (count %)))
            :type :string
            :description "A string spec with more than or equal to 8 characters."
            :swagger/example "test-password"}))

(def uuid?
  (st/spec {:spec (s/and string? #(re-matches uuid-regex %))
            :type :string
            :description "A string spec with UUID."
            :swagger/example "47d546e2-0e54-4eb5-9b27-abcdefabcdef"}))

(def vector-of-strings?
  (st/spec {:spec (s/coll-of string? :kind vector?)
            :description "A vector of strings"
            :swagger/example "[\"location\"]"}))