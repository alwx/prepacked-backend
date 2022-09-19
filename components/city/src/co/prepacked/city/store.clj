(ns co.prepacked.city.store)

(defn all-cities []
  (let [query {:select [:*]
               :from   [:city]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [key value]
  (let [query {:select [:*]
               :from   [:city]
               :where  [:= key value]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-slug [slug]
  (find-by :slug slug))
