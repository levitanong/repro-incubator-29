(ns repro.core
  (:require [fulcro.client :as fc]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro.client.network :as network]
            [fulcro.incubator.ui-state-machines :as uism :refer [defstatemachine]]))

(defonce app (atom nil))

(defstatemachine foo-fighters
  {::uism/actor-names #{:root}
   ::uism/states
   {:initial {::uism/events
              {::uism/started {::uism/target-state :default}}}
    :default {::uism/events
              {:do-stuff {::uism/handler (fn [env]
                                           (let [root-actor (uism/actor-class env :root)]
                                             (-> env
                                                 (uism/trigger-remote-mutation
                                                  :root
                                                  'repro.core/asdf
                                                  {;; ::uism/mutation-remote :remote
                                                   }))))}}}}})

(defsc Root
  [this props]
  {:ident         (fn [] [:component/by-id :root])
   :query         [:root/a]
   :initial-state (fn [p] {:root/a 1})}
  (dom/div
    (dom/button {:type    :button
                 :onClick (fn [e] (uism/trigger! this :foo-fighters :do-stuff))}
      "Trigger Remote Mutation")))

(defn start
  []
  (reset! app (fc/mount @app Root "app")))

(defn fulcro-init
  [{:keys [reconciler]}]
  (uism/begin! reconciler foo-fighters :foo-fighters
               {:root (uism/with-actor-class [:component/by-id :root] Root)}))

(defrecord Remote []
  network/NetworkBehavior
  (serialize-requests? [_] true)
  network/FulcroRemoteI
  (transmit [this {::network/keys [edn abort-id ok-handler error-handler] :as raw-request}]
    (js/console.log "raw-request" raw-request))
  (abort [this id]
    (js/console.log "aborting" id)))

(defn ^:export init []
  (reset! app (fc/make-fulcro-client
               {:started-callback fulcro-init
                :networking       {:remote (map->Remote {})}}))
  (start))
