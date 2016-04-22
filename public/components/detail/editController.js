/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model', 'uibButtonConfig', function($scope, $log, Model, btnConfig) {

	btnConfig.activeClass = 'btn-primary';

	var ctrl = this;

	ctrl.questions = [];
	ctrl.currentFeedbackList = [];
	ctrl.feedbackHistoryList = [];

	// get the pending actions
	Model.getQuestionSet().then(function(response) {
		ctrl.questions = response;
	});

	ctrl.save = function(feedbackId) {
		//todo
	};

	ctrl.cancel = function(feedbackId) {
		// $location.path = "/edit";
	}
}]);