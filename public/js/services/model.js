fbServices.service('Model', ['$log', '$q', 'Feedback', function($log, $q, Feedback) {

	var pendingActions = [];
	var currentFeedbackList = [];
	var feedbackHistoryList = [];
	var errorResult = undefined;

	var cacheServiceCall = function(mutateState, updateRequired, serviceFunction, serviceCallName) {
		var deferred = $q.defer();

		if ( updateRequired() ) {
			$log.debug("[" + serviceCallName + "] Updating from server...");
			serviceFunction().then(function(result){
				$log.debug("[" + serviceCallName + "] Response from server: [" + result + "]");
				deferred.resolve(mutateState(result));
			}, function(result){
				$log.error("[" + serviceCallName + "] Error from server:  [" + result + "]");
				errorResult(result.data);
			});
		} else {
			deferred.resolve(pendingActions);
		}
		return deferred.promise;
	}

	var model = {

		getPendingFeedbackActions: function(flushCache) {
			return cacheServiceCall(function(result) { return (pendingActions = result.data.body) }, 
									function() { return (pendingActions.length == 0 || flushCache)},
									Feedback.getPendingFeedbackActions, 
									"Feedback.getPendingFeedbackActions");
		},

		getCurrentFeedback: function(flushCache) {
			return cacheServiceCall(function(result) { return ( currentFeedbackList = result.data.body) },
									function() { return (currentFeedbackList.length == 0 || flushCache) },
									Feedback.getCurrentFeedbackItemsForSelf,
									"Feedback.getCurrentFeedbackItemsForSelf");
		},

		getFeedbackHistory: function(flushCache) {
			return cacheServiceCall(function(result) {return feedbackHistoryList = result.data.body },
									function() { return (feedbackHistoryList.length == 0 || flushCache)},
									Feedback.getFeedbackHistoryForSelf,
									"Feedback.getFeedbackHistoryForSelf");
		}
	}
	return model;
}]);