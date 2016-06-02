fbServices.service('Model', ['$log', '$q', 'Account', 'Feedback', 'Nomination', function($log, $q, Account, Feedback, Nomination) {

	var self = undefined;
	var pendingActions = [];
	var currentFeedbackList = [];
	var feedbackHistoryList = [];
	var feedbackDetail = {};
	var currentNominations = [];
	var nomineeCandidates = [];
	var feedbackCycles = [];
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

		getCurrentUser: function(flushCache) {
			return cacheServiceCall(function(result) { self = result.data.body },
									function() { return ( self == undefined || flushCache)},
									function() { return self },
									Account.getCurrentUser,
									"Account.getCurrentUser");
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
									function(feedbackId) {return ( feedbackDetail[feedbackId] == undefined || flushCache ) },
									function(feedbackId) { return feedbackDetail[feedbackId] },
									Feedback.getFeedbackItem,
									"Feedback.getFeedbackItem",
									feedbackId);
		},

		getNomineeCandidates: function(flushCache) {
			return cacheServiceCall(function(result) { nomineeCandidates = result.data.body.map(function(item){
											item.display = item.name + ", " + item.email;
											return item;
										});
									},
									function(feedbackId) {return ( !nomineeCandidates || nomineeCandidates.length == 0 || flushCache ) },
									function(feedbackId) { return nomineeCandidates },
									Nomination.getNomineeCandidates,
									"Nomination.getNomineeCandidates");
		},

		getCurrentNominations: function(flushCache) {
			return cacheServiceCall(function(result) { currentNominations = result.data.body },
									function(feedbackId) {return ( !currentNominations || currentNominations.length == 0 || flushCache ) },
									function(feedbackId) { return currentNominations },
									Nomination.getCurrentNominations,
									"Nomination.getCurrentNominations");
		},

		getActiveFeedbackCycles: function(flushCache) {
			return cacheServiceCall(function(result) { feedbackCycles = result.data.body },
									function(feedbackId) {return ( !feedbackCycles || feedbackCycles.length == 0 || flushCache ) },
									function(feedbackId) { return feedbackCycles },
									Feedback.getActiveFeedbackCycles,
									"Feedback.getActiveFeedbackCycles");
		},

		saveFeedback: function(feedbackItem, submit) {
			var deferred = $q.defer();
			$log.info(feedbackItem)
			$log.info(submit)
			if (!feedbackItem || !feedbackItem.id || !feedbackItem.questions) {
			$log.info("rejected...")
				deferred.reject();
			} else {
				$log.info("saving...")
				Feedback.updateFeedback(feedbackItem.id, feedbackItem.questions, !!submit).then(function() {
					$log.info("saved...")
					feedbackDetail[feedbackItem.id] = feedbackItem
					deferred.resolve();
				}, function(result) {
					$log.info("not save...")
					$log.info(result)
					deferred.reject();
				});
			}
			return deferred.promise;
		}
	}
	return model;
}]);