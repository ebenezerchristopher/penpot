;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns uxbox.main.ui.workspace.sidebar.libraries
  (:require
   [lentes.core :as l]
   [cuerdas.core :as str]
   [rumext.alpha :as mf]
   [uxbox.common.data :as d]
   [uxbox.builtins.icons :as i]
   [uxbox.main.data.workspace :as dw]
   [uxbox.main.refs :as refs]
   [uxbox.main.store :as st]
   [uxbox.main.ui.keyboard :as kbd]
   [uxbox.main.ui.shapes.icon :as icon]
   [uxbox.main.ui.workspace.sortable :refer [use-sortable]]
   [uxbox.util.dom :as dom]
   [uxbox.util.uuid :as uuid]
   [uxbox.util.i18n :as i18n :refer [t]]
   [uxbox.main.ui.components.tab-container :refer [tab-container tab-element]]
   [uxbox.main.data.library :as dlib]))

(def project-ref
  (-> (l/key :workspace-project)
      (l/derive st/state)))

(defn libraries-ref [section]
  (-> (comp (l/key :library) (l/key section))
      (l/derive st/state)))

(defn selected-items-ref [library-id]
  (-> (comp (l/key :library) (l/key :selected-items) (l/key library-id))
      (l/derive st/state)))

(mf/defc icons-tab [{:keys [libraries]}]
  (when (and libraries (-> libraries count (> 0)))
    (let [state (mf/use-state {:selected-library (-> libraries first :id)})]
      (mf/use-effect {:fn (fn []
                            (st/emit! (dlib/retrieve-library-data :icons (:selected-library @state))))
                      :deps (mf/deps (:selected-library @state))})

      [:div.library-tab.icons-tab
       [:select.input-select.library-tab-libraries
        {:on-change #(swap! state assoc :selected-library (-> % dom/get-target dom/get-value))}
        (for [library libraries]
          [:option.library-tab-libraries-item
           {:key (:id library)
            :value (:id library)}
           (:name library)])]
       [:div.library-tab-content
        (let [items (mf/deref (selected-items-ref (:selected-library @state)))]
          (for [item items]
            [:div.library-tab-element
             {:key (:id item)}
             [:svg {:view-box (->> item :metadata :view-box (str/join " "))
                    :width (-> item :metadata :width)
                    :height (-> item :metadat :height) 
                    :dangerouslySetInnerHTML {:__html (:content item)}}]
             [:span.library-tab-element-name (:name item)]]))]])))

(mf/defc images-tab [{:keys [libraries]}]
  (when (and libraries (-> libraries count (> 0)))
    (let [state (mf/use-state {:selected-library (-> libraries first :id)})]
      (mf/use-effect {:fn (fn []
                            (st/emit! (dlib/retrieve-library-data :images (:selected-library @state))))
                      :deps (mf/deps (:selected-library @state))})

      [:div.library-tab.images-tab
       [:select.input-select.library-tab-libraries
        {:on-change #(swap! state assoc :selected-library (-> % dom/get-target dom/get-value))}
        (for [library libraries]
          [:option.library-tab-libraries-item
           {:key (:id library)
            :value (:id library)}
           (:name library)])]
       [:div.library-tab-content
        (let [items (mf/deref (selected-items-ref (:selected-library @state)))]
          (for [item items]
            [:div.library-tab-element
             {:key (:id item)}
             [:img {:src (:thumb-uri item)}]
             [:span.library-tab-element-name (:name item)]]))]])))

(mf/defc libraries-toolbox
  [{:keys [key]}]
  (let [team-id (-> project-ref mf/deref :team-id)
        locale (i18n/use-locale)]
    (mf/use-effect {:fn (fn []
                          (st/emit! (dlib/retrieve-libraries :icons))
                          (st/emit! (dlib/retrieve-libraries :images)))
                    :deps (mf/deps key)})
    (mf/use-effect {:fn (fn []
                          (when team-id
                            (do 
                              (st/emit! (dlib/retrieve-libraries :icons team-id))
                              (st/emit! (dlib/retrieve-libraries :images team-id)))))
                    :deps (mf/deps team-id)})
    [:div#libraries.tool-window
     [:div.libraries-window-bar
      [:div.libraries-window-bar-title "Libraries"]
      [:div.libraries-window-bar-options
       "All libraries"
       [:button {:type "button"}
        i/arrow-slide]]]
     [:div.tool-window-content
      [:& tab-container {}
       [:& tab-element
        {:id :icons :title "Icons"}
        [:& icons-tab {:libraries (-> (libraries-ref :icons) mf/deref vals flatten) }]]

       [:& tab-element
        {:id :images :title "Images"}
        [:& images-tab {:libraries (-> (libraries-ref :images) mf/deref vals flatten)}]]]]]))

