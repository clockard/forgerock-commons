'use strict';

angular.module('localization', [])

    .factory('localize', ['$http', '$rootScope', '$window', '$filter', function ($http, $rootScope, $window, $filter) {
        var localize = {
            language:'',
            dictionary:[],
            url: undefined,
            resourceFileLoaded:false,
            successCallback:function (data) {
                localize.dictionary = data;
                localize.resourceFileLoaded = true;
                $rootScope.$broadcast('localizeResourcesUpdated');
            },
            setLanguage: function(value) {
                localize.language = value;
                localize.initLocalizedResources();
            },
            setUrl: function(value) {
                localize.url = value;
                localize.initLocalizedResources();
            },
            buildUrl: function() {
                if(!localize.language){
                    var lang, androidLang;
                    if ($window.navigator && $window.navigator.userAgent && (androidLang = $window.navigator.userAgent.match(/android.*\W(\w\w)-(\w\w)\W/i))) {
                        lang = androidLang[1];
                    } else {
                        lang = $window.navigator.userLanguage || $window.navigator.language;
                    }
                    localize.language = lang;
                }
                return 'js/i18n/resources-locale_' + localize.language + '.js';
            },
            initLocalizedResources:function () {
                var url = localize.url || localize.buildUrl();
                $http({ method:"GET", url:url, cache:false }).success(localize.successCallback).error(function () {
                    var url = 'js/i18n/resources-locale_en-US.js';
                    $http({ method:"GET", url:url, cache:false }).success(localize.successCallback);
                });
            },
            getLocalizedString: function(value) {
                var result = '';
                if ((localize.dictionary !== []) && (localize.dictionary.length > 0)) {
                    var entry = $filter('filter')(localize.dictionary, function(element) {
                            return element.key === value;
                        }
                    )[0];
                    result = entry.value;
                }
                return result;
            }
        };
        localize.initLocalizedResources();
        return localize;
    } ])
    // simple translation filter
    // usage {{ TOKEN | i18n }}
    .filter('i18n', ['localize', function (localize) {
        return function (input) {
            return localize.getLocalizedString(input);
        };
    }])
    // translation directive that can handle dynamic strings
    // updates the text value of the attached element
    // usage <span data-i18n="TOKEN" ></span>
    // or
    // <span data-i18n="TOKEN|VALUE1|VALUE2" ></span>
    .directive('i18n', ['localize', function(localize){
        var i18nDirective = {
            restrict:"EAC",
            updateText:function(elm, token){
                var values = token.split('|');
                if (values.length >= 1) {
                    var tag = localize.getLocalizedString(values[0]);
                    if ((tag !== null) && (tag !== undefined) && (tag !== '')) {
                        if (values.length > 1) {
                            for (var index = 1; index < values.length; index++) {
                                var target = '{' + (index - 1) + '}';
                                tag = tag.replace(target, values[index]);
                            }
                        }
                        elm.text(tag);
                    };
                }
            },

            link:function (scope, elm, attrs) {
                scope.$on('localizeResourcesUpdated', function() {
                    i18nDirective.updateText(elm, attrs.i18n);
                });

                attrs.$observe('i18n', function (value) {
                    i18nDirective.updateText(elm, attrs.i18n);
                });
            }
        };

        return i18nDirective;
    }])
    // translation directive that can handle dynamic strings
    // updates the attribute value of the attached element
    // usage <span data-i18n-attr="TOKEN|ATTRIBUTE" ></span>
    // or
    // <span data-i18n-attr="TOKEN|ATTRIBUTE|VALUE1|VALUE2" ></span>
    .directive('i18nAttr', ['localize', function (localize) {
        var i18NAttrDirective = {
            restrict: "EAC",
            updateText:function(elm, token){
                var values = token.split('|');
                var tag = localize.getLocalizedString(values[0]);
                if ((tag !== null) && (tag !== undefined) && (tag !== '')) {
                    if (values.length > 2) {
                        for (var index = 2; index < values.length; index++) {
                            var target = '{' + (index - 2) + '}';
                            tag = tag.replace(target, values[index]);
                        }
                    }
                    elm.attr(values[1], tag);
                }
            },
            link: function (scope, elm, attrs) {
                scope.$on('localizeResourcesUpdated', function() {
                    i18NAttrDirective.updateText(elm, attrs.i18nAttr);
                });

                attrs.$observe('i18nAttr', function (value) {
                    i18NAttrDirective.updateText(elm, value);
                });
            }
        };

        return i18NAttrDirective;
    }]);