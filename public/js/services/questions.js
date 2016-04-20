fbServices.service('Questions', ['$log','$http', function($log, $http) {

	return {
		getQuestionSet: function (questionSetId) {
			return $http.get("/api/questions/" + questionSetId);
		}

	}
}]);