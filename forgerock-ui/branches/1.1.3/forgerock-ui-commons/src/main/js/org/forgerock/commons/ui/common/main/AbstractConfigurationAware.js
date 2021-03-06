/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*global require define*/

/**
 * @author yaromin
 */
define("org/forgerock/commons/ui/common/main/AbstractConfigurationAware", [
], function(globalConfiguration) {

    var obj = function AbstractConfigurationAware() {
    };

    obj.prototype.updateConfigurationCallback = function(configuration) {
        var configurationSetsToLoad = [], i, key, itemKey, singleConfigurationData, confKey, confToAdd;
        console.debug('Configuration updated');
        this.configuration = configuration;
        
        if (configuration.loader) {
            configurationSetsToLoad = configuration.loader;
            
            for (key in configurationSetsToLoad) {
                
                if (configurationSetsToLoad.hasOwnProperty(key)) {
                    
                      singleConfigurationData = configurationSetsToLoad[key];
                      for (itemKey in singleConfigurationData) {
                          if (singleConfigurationData.hasOwnProperty(itemKey)) {
                              confToAdd = require(singleConfigurationData[itemKey]);
                              for (confKey in confToAdd) {
                                  
                                  if (!this.configuration[itemKey][confKey]) {
                                      this.configuration[itemKey][confKey] = confToAdd[confKey];
                                  }
                              }
                          }
                      }
                      
                }
            }
        }
        
    };

    return obj;
});