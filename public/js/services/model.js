fbServices.service('Model', ['$log', '$q', function($log, $q) {

	var pendingActions = [];

	return {
		getPendingFeedbackActions: function() {

			var deferred = $q.defer();
			deferred.resolve([]);
			return deferred.promise;
		}
	}
}]);