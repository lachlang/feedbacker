fbServices.service('Feedback', ['$log','$http', function($log, $http) {

	return {
		getPendingFeedbackActions: function () {
			return $http.get("/api/feedback/pending");
		},

		createNewFeedbackItem: function(id, feedback) {
			return $http({
				method: "POST",
				url: "/api/feedback/new/" + id,
				data:{
					apiVersion: "1.0",
					body: feedback
				}
			});
		},

		updateFeedbackItem: function(id, feedback) {
			return $http({
				method:"PUT",
				url:"/api/feedback/update/" + id,
				data: {
					apiVersion: "1.0",
					body: feedback
				}
			});
		},

		getFeedbackItem: function(id) {
			return $http.get("/api/feedback/item/" + id)
		},

		getCurrentFeedbackItemsForUser: function(id) {
			return $http.get("/api/feedback/current/" + id);
		},

		getCurrentFeedbackItemsForSelf: function() {
			return $http.get("/api/feedback/current/self");
		},

		getFeedbackHistoryForUser: function(id) {
			return $http.get("/api/feedback/history/" + id);
		},
		getFeedbackHistoryForSelf: function() {
			return $http.get("/api/feedback/history/self");
		}
	}
}]);