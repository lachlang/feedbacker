fbServices.service('Model', ['$log', '$q', 'Feedback', function($log, $q, Feedback) {

	var pendingActions = [];
	var currentFeedbackList = [];
	var feedbackHistoryList = [];
	var feedbackDetail = {};
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

		getPendingFeedbackActions: function(flushCache) {
			return cacheServiceCall(function(result) { pendingActions = result.data.body }, 
									function() { return (pendingActions.length == 0 || flushCache)},
									function() { return pendingActions },
									Feedback.getPendingFeedbackActions, 
									"Feedback.getPendingFeedbackActions");
		},

		getCurrentFeedback: function(flushCache) {
			return cacheServiceCall(function(result) { currentFeedbackList = result.data.body },
									function() { return (currentFeedbackList.length == 0 || flushCache) },
									function() { return currentFeedbackList },
									Feedback.getCurrentFeedbackItemsForSelf,
									"Feedback.getCurrentFeedbackItemsForSelf");
		},

		getFeedbackHistory: function(flushCache) {
			return cacheServiceCall(function(result) { feedbackHistoryList = result.data.body },
									function() { return (feedbackHistoryList.length == 0 || flushCache)},
									function() { return feedbackHistoryList },
									Feedback.getFeedbackHistoryForSelf,
									"Feedback.getFeedbackHistoryForSelf");
		},

		getFeedbackDetail: function(feedbackId, flushCache) {
			return cacheServiceCall(function(result, feedbackId) { feedbackDetail[feedbackId] = result.data.body },
									function(feedbackId) {return ( feedbackDetail[feedbackId] == undefined || flushCache ) },
									function(feedbackId) { return feedbackDetail[feedbackId] },
									Feedback.getFeedbackItem,
									"Feedback.getFeedbackItem",
									feedbackId);
		},

		saveFeedback: function() {
			var deferred = $q.defer;
			deferred.resolve();
			return $deferred.promise;
		}
	}
	return model;
}]);