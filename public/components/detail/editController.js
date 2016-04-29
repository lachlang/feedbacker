/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model', 'uibButtonConfig', '$location', function($scope, $log, Model, btnConfig, $location) {

	btnConfig.activeClass = 'btn-primary';

	var ctrl = this;

	ctrl.questions = [];
	ctrl.error = undefined;
	ctrl.feedbackForName = undefined;
	ctrl.managerName = undefined;
	ctrl.shareFeedback = false;

	ctrl.initialiseController = function() {
		var feedbackId = $location.search()["id"];

		// TODO: fix this when I'm not on the plane
		// if (feedbackId && Number.isInteger(feedbackId)) {
		if (feedbackId) {

			Model.getFeedbackDetail(feedbackId).then(function(response) {
				$log.info(response)
				ctrl.questions = response.questions;
				ctrl.feedbackForName = response.feedbackForName;
				ctrl.managerName = response.managerName;
				if (response.shareFeedback) {
					ctrl.shareFeedback = response.shareFeedback;
				}
			});
		} else {
			error = "Invalid link."
		}
	}

	ctrl.save = function(feedbackId) {
		Model.saveFeedback().then(function(response) {

			$location.path("/list");
		});
	};

	ctrl.cancel = function() {
		$location.path("/list");
	}

	ctrl.initialiseController();
}]);