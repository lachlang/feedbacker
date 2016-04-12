fbServices.service('Feedback', ['$log','$http', function($log, $http) {

	return {
		getPendingFeedbackActions: function () {
			$http.get("api/feedback/pending");
		}
	}
}]);