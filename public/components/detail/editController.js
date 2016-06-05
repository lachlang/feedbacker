/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model', 'uibButtonConfig', '$location', function($scope, $log, Model, btnConfig, $location) {

	btnConfig.activeClass = 'btn-primary';

	var ctrl = this;

	ctrl.error = undefined;
	ctrl.message = undefined;
	ctrl.feedback = {questions: []};

	ctrl.initialiseController = function() {
		var feedbackId = $location.search()["id"];

		// TODO: fix this when I'm not on the plane
		// if (feedbackId && Number.isInteger(feedbackId)) {
		if (feedbackId) {

			Model.getFeedbackDetail(feedbackId).then(function(response) {
				ctrl.feedback = response;
				if (!ctrl.feedback.shareFeedback) {
					ctrl.feedback.shareFeedback = false;
				}
			}, function(response) {
				// error condition
				ctrl.error = "Could not load feedback.  Please try again later.";
			});
		} else {
			ctrl.error = "Couldn't load feedback."
		}
	}

	ctrl.save = function(feedback, submit) {
		ctrl.resetError();

		Model.saveFeedback(feedback, submit).then(function(response) {
			if (submit) {
				ctrl.navigateToList();
			} else {
				ctrl.message = "Saved feedback."
			}
		}, function() {
			ctrl.error = "Could not save feedback.  Please try again later."
		});
	};

	ctrl.initialiseController();

	ctrl.navigateToList = function() {
//		$location.search("id", undefined);
		$location.path("list");
	};

	ctrl.resetError = function() {
		ctrl.error = undefined;
		ctrl.message = undefined;
	};
}]);