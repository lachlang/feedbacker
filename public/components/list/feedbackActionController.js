/*
 * Controller for feedback action
 */
fbControllers.controller('FeedbackActionCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

	ctrl.user = {};
	ctrl.pendingActions = [];
	ctrl.currentFeedbackList = [];
	ctrl.feedbackHistoryList = [];

	// get the pending actions
	Model.getCurrentUser().then(function(response) {
		ctrl.user = response;
	});

	Model.getPendingFeedbackActions().then(function(response) {
		ctrl.pendingActions = response;
	});

	Model.getCurrentFeedback().then(function(response) {
		ctrl.currentFeedbackList = response;
	});

	Model.getFeedbackHistory().then(function(response) {
		ctrl.feedbackHistoryList = response;
	});

	ctrl.viewFeedbackDetail = function(feedbackId) {
		// $location.path("/detailView");
	};

	ctrl.editFeedbackDetail = function(feedbackId) {
		// $location.path("/detailEdit");
	};
}]);