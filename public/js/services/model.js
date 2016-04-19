fbServices.service('Model', ['$log', '$q', 'Feedback', function($log, $q, Feedback) {

	var pendingActions = [];
	var currentFeedbackList = [];
	var feedbackHistoryList = [];
	var errorResult = undefined;

	var cacheServiceCall = function(mutateCache, updateRequired, getCache, serviceFunction, serviceCallName) {
		var deferred = $q.defer();

		if ( updateRequired() ) {
			$log.debug("[" + serviceCallName + "] Updating from server...");
			serviceFunction().then(function(result){
				$log.debug("[" + serviceCallName + "] Response from server: [" + result + "]");
				mutateCache(result);
				deferred.resolve(getCache());
			}, function(result){
				$log.error("[" + serviceCallName + "] Error from server:  [" + result + "]");
				errorResult(result.data);
			});
		} else {
			deferred.resolve(getCache());
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
		}
	}
	return model;
}]);