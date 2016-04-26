/*
 * Controller for feedback action
 */
fbControllers.controller('FeedbackActionCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

	ctrl.actions = [];
	ctrl.currentFeedbackList = [];
	ctrl.feedbackHistoryList = [];

	// get the pending actions
	Model.getPendingFeedbackActions().then(function(response) {
		ctrl.actions = response;
	});

	// Model.getCurrentFeedback().then(function(response) {
	// 	ctrl.currentFeedbackList = response;
	// });

	// Model.getFeedbackHistory().then(function(response) {
	// 	ctrl.feedbackHistoryList = response;
	// });

	ctrl.viewFeedbackDetail = function(feedbackId) {
		//todo
	};

	ctrl.editFeedbackDetail = function(feedbackId) {
		// $location.path = "/edit";
	}
}]);