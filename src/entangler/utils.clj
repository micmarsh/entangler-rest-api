(ns entangler.utils)

(defn get-map-converter [translator]
    (fn [from]
        (let [unmerged (for [[key fn] translator]
                {key (fn from)})]
        (apply merge unmerged))))