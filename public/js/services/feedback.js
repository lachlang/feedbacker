fbServices.service('Feedback', ['$log','$http', function($log, $http) {

	return {
		getPendingFeedbackActions: function () {
			return $http.get("/api/feedback/pending");
		},

		updateFeedback: function(feedbackId, feedbackResponses, submit) {
			return $http({
				method:"PUT",
				url:"/api/feedback/item/" + feedbackId,
				data: {
					apiVersion: "1.0",
					body: {
						questions: feedbackResponses,
						submit: submit
					}
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
		},

		getActiveFeedbackCycles: function() {
			return $http.get("/api/cycle/active");
		},

		getFeedbackCycle: function(cycleId) {
			return $http.get("/api/cycle/" + cycleId);
		},

		createAdHocFeedback: function(recipient, message, publishToRecipient) {
		  return $http({
		    method: 'POST',
		    url: "/api/feedback/adhoc",
		    data: {
					apiVersion: "1.0",
					body: {
						recipientEmail: recipient,
						message: message,
						publishToRecipient: publishToRecipient
					}
		    }
      });
		},

		getAdHocFeedbackFor: function(username) {
		  return $http.get("/api/feedback/adhoc/for/" + username)
		},

		getAdHocFeedbackFrom: function() {
		  return $http.get("/api/feedback/adhoc/from")
		}
	}
}]);