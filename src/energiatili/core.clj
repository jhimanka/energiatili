(ns energiatili.core
  (:require
   [clj-http.core]
   [clj-http.cookies]
   [clj-http.client :as client]
   [jsonista.core :as j]
   [clojure.string :as str])
  (:gen-class))

(def alldata? (or (System/getenv "ETALLDATA") false))
(def etuser (System/getenv "ETUSER"))
(def etpw (System/getenv "ETPW"))
(def dburl (or (System/getenv "DBURL") "http://localhost:8086/write?db=raksila&precision=ms"))

(defn -main
  []
  (as-> (binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)]
          (client/get "https://www.energiatili.fi/Extranet/Extranet")
          (client/post "https://www.energiatili.fi/Extranet/Extranet/LogIn"
                       {:form-params {:username etuser
                                      :password etpw}})
          (:body (client/get "https://www.energiatili.fi/Reporting/CustomerConsumption/UserConsumptionReport"))) x
    (last (re-find #"(?s)var model = (.+});\r\n\r\n +var GraphContext" x)) ; etsi data sivun sisältä
    (str/replace x #"new Date.([-\d]+)." "$1") ; korjaa rikkinäiset aikaleimat
    (j/read-value x) ; jäsennä JSON
    (mapv #(get-in % ["Series" "Data"]) (get-in x ["Days" "Consumptions"])) ; päiväkohtainen data
    (mapv (fn [i & args] ; korjataan virheellistä aikaleimaa 3t ja tarvittaessa summataan yö- ja päiväsähköt
            [(- (first i) 10800000) (if (nil? args) (last i) (+ (last i) (last (first args))))])
          (first x) (when (vector? (second x)) (second x)))
    (if alldata? x [(last x)]) ; joko koko historia tai viimeisin tieto
    (reduce (fn [acc item]
              (str acc (format "raksilakwh,sensor=kwh value=%.1f %d\n", (last item) (first item)))) "" x)
    (client/post dburl {:body x :content-type :application/x-www-form-urlencoded})))
