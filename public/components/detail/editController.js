/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model', 'uibButtonConfig', '$location', function($scope, $log, Model, btnConfig, $location) {

	btnConfig.activeClass = 'btn-primary';

	var ctrl = this;

	ctrl.questions = [];
	ctrl.error = undefined;
	ctrl.feedbackForName = undefined;
	ctrl.feedbackFromName = undefined;
	ctrl.managerName = undefined;
	ctrl.shareFeedback = false;

	ctrl.initialiseController = function() {
		var feedbackId = $location.search()["id"];

		// TODO: fix this when I'm not on the plane
		// if (feedbackId && Number.isInteger(feedbackId)) {
		if (feedbackId) {

			Model.getFeedbackDetail(feedbackId).then(function(response) {
				ctrl.questions = response.questions;
				ctrl.feedbackForName = response.feedbackForName;
				ctrl.feedbackFromName = response.feedbackFromName;
				ctrl.managerName = response.managerName;
				if (response.shareFeedback) {
					ctrl.shareFeedback = response.shareFeedback;
				}
			}, function(response) {
				// error condition
				ctrl.error = "Could not load feedback.  Please try again later.";
			});
		} else {
			ctrl.error = "Couldn't load feedback."
		}
	}

	ctrl.save = function(feedbackId) {
		Model.saveFeedback().then(function(response) {

			ctrl.navigateToList();
		});
	};

	ctrl.cancel = function() {

		ctrl.navigateToList();
	};

	ctrl.initialiseController();

	ctrl.navigateToList = function() {
		ctrl.resetError();
		$location.search("id", undefined);
		$location.path("/list");
	};

	ctrl.resetError = function() {
		ctrl.error = undefined;
	};
}]);