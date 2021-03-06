fbServices.service('Model', ['$log', '$q', 'Account', 'Feedback', 'Nomination', function($log, $q, Account, Feedback, Nomination) {

  var self = undefined;
  var pendingActions = [];
  var currentFeedbackList = [];
  var feedbackHistoryList = [];
  var feedbackDetail = {};
  var feedbackCycle = {};
  var currentNominations = [];
  var activeUsers = [];
  var registeredUsers = [];
  var feedbackCandidates = [];
  var activeFeedbackCycles = [];
  var allFeedbackCycles = [];
  var userReports = [];
  var cycleReports = [];
  var adHocFeedbackForUser = {};
  var adHocFeedbackForSelf = [];
  var adHocFeedbackFromSelf = [];
  var errorResult = undefined;

  var cacheServiceCall = function(mutateCache, updateRequired, getCache, serviceFunction, serviceCallName, mapKey) {
    var deferred = $q.defer();

    if ( updateRequired(mapKey) ) {
      $log.debug("[" + serviceCallName + "] Updating from server...");
      serviceFunction(mapKey).then(function(result){
        $log.debug("[" + serviceCallName + "] Response from server...");
        $log.debug(result)
        mutateCache(result, mapKey);
        deferred.resolve(getCache(mapKey));
      }, function(result){
        $log.error("[" + serviceCallName + "] Error from server:  [" + result + "]");
        errorResult = result.data;
        deferred.reject(errorResult);
      });
    } else {
      deferred.resolve(getCache(mapKey));
    }
    return deferred.promise;
  }

  var model = {

        flush: function() {
            self = undefined;
            pendingActions = [];
            currentFeedbackList = [];
            feedbackHistoryList = [];
            feedbackDetail = {};
            feedbackCycle = {};
            currentNominations = [];
            activeUsers = [];
            registeredUsers = [];
            feedbackCandidates = [];
            activeFeedbackCycles = [];
            allFeedbackCycles = [];
            userReports = [];
            cycleReports = [];
            adHocFeedbackForUser = {};
            adHocFeedbackForSelf = [];
            adHocFeedbackFromSelf = [];
            errorResult = undefined;
        },

    getCurrentUser: function(flushCache) {
      return cacheServiceCall(function(result) { self = result.data.body },
                  function() { return ( self == undefined || flushCache)},
                  function() { return self },
                  Account.getCurrentUser,
                  "Account.getCurrentUser");
    },

    getUserReports: function(flushCache) {
      return cacheServiceCall(function(result) { userReports = result.data.body },
                  function() {return ( !userReports || userReports.length == 0 || flushCache ) },
                  function() { return userReports },
                  Account.getUserReports,
                  "Account.getUserReports");
    },

    getCycleReports: function(flushCache) {
      return cacheServiceCall(function(result) { cycleReports = result.data.body },
                  function() {return ( !cycleReports || cycleReports.length == 0 || flushCache ) },
                  function() { return cycleReports },
                  Feedback.getCycleReports,
                  "Feedback.getCycleReports");
    },

    updateCurrentUser: function(name, role) {
      var deferred = $q.defer();

      $log.debug("[Account.updateCurrentUser] Updating to server...");
      Account.updateCurrentUser(name, role).then(function(result){
        $log.debug("[Account.updateCurrentUser] Response from server...");
        $log.debug(result)
        self = result.data.body;
        deferred.resolve(self);
      }, function(result){
        $log.error("[Account.updateCurrentUser] Error from server:  [" + result + "]");
        errorResult = result.data;
        deferred.reject(errorResult);
      });
      return deferred.promise;
    },

    getPendingFeedbackActions: function(flushCache) {
      return cacheServiceCall(function(result) { pendingActions = result.data.body },
                  function() { return (!pendingActions || pendingActions.length == 0 || flushCache)},
                  function() { return pendingActions },
                  Feedback.getPendingFeedbackActions,
                  "Feedback.getPendingFeedbackActions");
    },

    getCurrentFeedback: function(flushCache) {
      return cacheServiceCall(function(result) { currentFeedbackList = result.data.body },
                  function() { return (!currentFeedbackList || currentFeedbackList.length == 0 || flushCache) },
                  function() { return currentFeedbackList },
                  Feedback.getCurrentFeedbackItemsForSelf,
                  "Feedback.getCurrentFeedbackItemsForSelf");
    },

    getFeedbackHistory: function(flushCache) {
      return cacheServiceCall(function(result) { feedbackHistoryList = result.data.body },
                  function() { return (!feedbackHistoryList || feedbackHistoryList.length == 0 || flushCache)},
                  function() { return feedbackHistoryList },
                  Feedback.getFeedbackHistoryForSelf,
                  "Feedback.getFeedbackHistoryForSelf");
    },

    getFeedbackDetail: function(feedbackId, flushCache) {
      return cacheServiceCall(function(result, feedbackId) { feedbackDetail[feedbackId] = result.data.body },
                  function(feedbackId) { return ( feedbackDetail[feedbackId] == undefined || flushCache ) },
                  function(feedbackId) { return feedbackDetail[feedbackId] },
                  Feedback.getFeedbackItem,
                  "Feedback.getFeedbackItem",
                  feedbackId);
    },

    updateFeedbackDetail: function(id, questions, shareFeedback, submit) {
      var deferred = $q.defer();

      $log.debug("[Feedback.updateFeedback] Updating to server...");
      Feedback.updateFeedback(id, questions, !!shareFeedback, !!submit).then(function(result){
        $log.debug("[Feedback.updateFeedback] Response from server...");
        $log.debug(result)
        feedbackDetail[result.data.body.id] = result.data.body;
        deferred.resolve(feedbackDetail[result.data.body.id]);
      }, function(result){
        $log.error("[Feedback.updateFeedback] Error from server:  [" + result + "]");
        errorResult = result.data;
        deferred.reject(errorResult);
      });
      return deferred.promise;
    },

    getActiveUsers: function(flushCache) {
      return cacheServiceCall(function(result) { activeUsers = result.data.body.map(function(item){
                      item.display = item.name + " (" + item.email + ")";
                      return item;
                    });
                  },
                  function() { return ( !activeUsers || activeUsers.length == 0 || flushCache ) },
                  function() { return activeUsers },
                  Account.getActiveUsers,
                  "Account.getActiveUsers");
    },

    getRegisteredUsers: function(flushCache) {
      return cacheServiceCall(function(result) { registeredUsers = result.data.body.map(function(item){
                      item.display = item.name + " (" + item.email + ")";
                      return item;
                    });
                  },
                  function() { return ( !registeredUsers || registeredUsers.length == 0 || flushCache ) },
                  function() { return registeredUsers },
                  Account.getRegisteredUsers,
                  "Account.getRegisteredUsers");
    },

    getCurrentNominations: function(flushCache) {
      return cacheServiceCall(function(result) { currentNominations = result.data.body },
                  function() { return ( !currentNominations || currentNominations.length == 0 || flushCache ) },
                  function() { return currentNominations },
                  Nomination.getCurrentNominations,
                  "Nomination.getCurrentNominations");
    },

    getActiveFeedbackCycles: function(flushCache) {
      return cacheServiceCall(function(result) { activeFeedbackCycles = result.data.body },
                  function() { return ( !activeFeedbackCycles || activeFeedbackCycles.length == 0 || flushCache ) },
                  function() { return activeFeedbackCycles },
                  Feedback.getActiveFeedbackCycles,
                  "Feedback.getActiveFeedbackCycles");
    },

    getAllFeedbackCycles: function(flushCache) {
      return cacheServiceCall(function(result) { allFeedbackCycles = result.data.body },
                  function() { return ( !activeFeedbackCycles || allFeedbackCycles.length == 0 || flushCache ) },
                  function() { return allFeedbackCycles },
                  Feedback.getAllFeedbackCycles,
                  "Feedback.getAllFeedbackCycles");
    },

    getFeedbackCycle: function(cycleId, flushCache) {
      return cacheServiceCall(function(result) { feedbackCycle[cycleId] = result.data.body },
                  function() { return ( feedbackCycle[cycleId] == undefined || flushCache ) },
                  function() { return feedbackCycle[cycleId] },
                  Feedback.getFeedbackCycle,
                  "Feedback.getFeedbackCycle",
                  cycleId);
    },

    createAdHocFeedback: function(recipientEmail, message, publishToRecipient) {
      var deferred = $q.defer();

      $log.debug("[Feedback.createAdHocFeedback] Updating to server...");
      Feedback.createAdHocFeedback(recipientEmail, message, publishToRecipient).then(function(result){
        $log.debug("[Feedback.createAdHocFeedback] Response from server...");
        $log.debug(result)
        adHocFeedbackFromSelf.push(result.data.body);
        deferred.resolve(adHocFeedbackFromSelf);
      }, function(result){
        $log.error("[Feedback.createAdHocFeedback] Error from server:  [" + result + "]");
        errorResult = result.data;
        deferred.reject(errorResult);
      });
      return deferred.promise;
    },

    getAdHocFeedbackForUser: function(recipientEmail, flushCache) {
      return cacheServiceCall(function(result) { adHocFeedbackForUser[recipientEmail] = result.data.body },
                  function(recipientEmail) { return (!adHocFeedbackForUser[recipientEmail] || flushCache)},
                  function(recipientEmail) { return adHocFeedbackForUser[recipientEmail] },
                  Feedback.getAdHocFeedbackForUser,
                  "Feedback.getAdHocFeedbackForUser",
                  recipientEmail);
    },

    getAdHocFeedbackForSelf: function(flushCache) {
      return cacheServiceCall(function(result) { adHocFeedbackForSelf = result.data.body },
                  function(recipientEmail) { return (!adHocFeedbackForSelf || adHocFeedbackForSelf.length == 0 || flushCache)},
                  function(recipientEmail) { return adHocFeedbackForSelf },
                  Feedback.getAdHocFeedbackForSelf,
                  "Feedback.getAdHocFeedbackForSelf");
    },

    getSubmittedAdHocFeedback: function(flushCache) {
      return cacheServiceCall(function(result) { adHocFeedbackFromSelf = result.data.body },
                  function() { return (!adHocFeedbackFromSelf || adHocFeedbackFromSelf.length == 0 || flushCache)},
                  function() { return adHocFeedbackFromSelf },
                  Feedback.getAdHocFeedbackFromSelf,
                  "Feedback.getAdHocFeedbackFromSelf");
    },

    createFeedbackCycle: function(cycle) {
      var deferred = $q.defer();

      $log.debug("[Feedback.createFeedbackCycle] Updating to server...");
      Feedback.createFeedbackCycle(cycle).then(function(result){
        $log.debug("[Feedback.createFeedbackCycle] Response from server...");
        $log.debug(result)
        feedbackCycle[result.data.body.id] = result.data.body;
        allFeedbackCycles.push(feedbackCycle[result.data.body.id]);
        deferred.resolve(feedbackCycle[result.data.body.id]);
      }, function(result){
        $log.error("[Feedback.createFeedbackCycle] Error from server:  [" + result + "]");
        errorResult = result.data;
        deferred.reject(errorResult);
      });
      return deferred.promise;
    },

    updateFeedbackCycle: function(cycle) {
      var deferred = $q.defer();

      $log.debug("[Feedback.updateFeedbackCycle] Updating to server...");
      Feedback.updateFeedbackCycle(cycle).then(function(result){
        $log.debug("[Feedback.updateFeedbackCycle] Response from server...");
        $log.debug(result)
        feedbackCycle[result.data.body.id] = result.data.body;
        deferred.resolve(feedbackCycle[result.data.body.id]);
      }, function(result){
       $log.error("[Feedback.updateFeedbackCycle] Error from server:  [" + result + "]");
        errorResult = result.data;
        deferred.reject(errorResult);
      });
      return deferred.promise;
    }

  }

  return model;
}]);
