(ns cljcc.core
  (:require [clojure.string :as str]))

(def regex-main #"int main\s*\(\s*\)\s*\{\s*return\s+(?<ret>[0-9]+)\s*;\s*\}")

(def assembly-template "
    .globl main
main:
    movl    $%d, %%eax
    ret
")

(defn init-asm
  [asm-file]
  (spit asm-file ""))

(defn emit-asm
  [asm-file code & args]
  (let [formatted-str (apply format code args)]
    (spit asm-file formatted-str :append true)))

(defn read-file
  [source-file]
  (slurp source-file))

(defn compile-program
  [asm-file code]
  (let [matcher (re-matcher regex-main code)
        retval  (if (.matches matcher)
                  {:ret (.group matcher "ret")}
                  (throw (ex-info (str "Can't extract ret value! ") {:code code})))]
    (init-asm asm-file)
    (emit-asm asm-file assembly-template (Integer/parseInt (:ret retval)))))

(defn get-asm-file-name
  [file-with-ext]
  (str (first (str/split file-with-ext #"\.")) ".s"))

(defn -main
  [& args]
  (let [source-file (first args)
        asm-file    (get-asm-file-name source-file)
        source-code (->
                      source-file
                      read-file
                      str/trim)]
    (compile-program asm-file source-code)))
