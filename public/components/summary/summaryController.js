/*
 * Controller for feedback action
 */
fbControllers.controller('SummaryCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

	ctrl.user = {};
	ctrl.currentFeedbackList = [];
	ctrl.feedbackHistoryList = [];

	// get the pending actions
	Model.getCurrentUser().then(function(response) {
		ctrl.user = response;
	});

	Model.getCurrentFeedback().then(function(response) {
		ctrl.currentFeedbackList = response;
	});

	Model.getFeedbackHistory().then(function(response) {
		ctrl.feedbackHistoryList = response;
	});
}]);