fbServices.service('Feedback', ['$log','$http', function($log, $http) {

	return {
		getPendingFeedbackActions: function () {
			return $http.get("/api/feedback/pending");
		},

		createNewFeedbackItem: function(feedbackId, feedback) {
			return $http({
				method: "POST",
				url: "/api/feedback/new/" + feedbackId,
				data:{
					apiVersion: "1.0",
					body: feedback
				}
			});
		},

		updateFeedbackItem: function(feedbackId, feedback) {
			return $http({
				method:"PUT",
				url:"/api/feedback/update/" + feedbackId,
				data: {
					apiVersion: "1.0",
					body: feedback
				}
			});
		},

		getFeedbackItem: function(feedbackId) {
			return $http.get("/api/feedback/item/" + feedbackId)
		},

		getCurrentFeedbackItemsForUser: function(personId) {
			return $http.get("/api/feedback/current/" + personId);
		},

		getCurrentFeedbackItemsForSelf: function() {
			return $http.get("/api/feedback/current/self");
		},

		getFeedbackHistoryForUser: function(personId) {
			return $http.get("/api/feedback/history/" + personId);
		},
		getFeedbackHistoryForSelf: function() {
			return $http.get("/api/feedback/history/self");
		}
	}
}]);