fbServices.service('Model', ['$log', '$q', 'Feedback', function($log, $q, Feedback) {

	var pendingActions = [];
	var errorResult = undefined;

	var model = {

		getPendingFeedbackActions: function(flushCache) {
			var deferred = $q.defer();

			if (pendingActions.lenth == 0 || flushCache) {
				$log.debug("[Model.getPendingFeedbackActions] Updating from server...");
				Feedback.getPendingFeedbackActions.then(function(result){
					$log.debug("[Model.getPendingFeedbackActions] Response from server: [" + result + "]");
					pendingActions = result.data.data.pendingActions;
					deferred.resolve(pendingActions);
				}, function(result){
					$log.error("[Model.getPendingFeedbackActions] Error from server:  [" + result + "]");
					errorResult(result.data);
				});
			} else {
				deferred.resolve(pendingActions);
			}
			return deferred.promise;
		}
	}
	return model;
}]);