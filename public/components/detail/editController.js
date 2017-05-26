/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model','Feedback', 'uibButtonConfig', '$location', function($scope, $log, Model, Feedback, btnConfig, $location) {

	btnConfig.activeClass = 'btn-primary';

	var ctrl = this;

	ctrl.error = undefined;
	ctrl.message = undefined;
	ctrl.feedback = {questions: []};

	ctrl.initialiseController = function() {
		var feedbackId = $location.search()["id"];

		// TODO: fix this when I'm not on the plane
        //if (feedbackId && Number.isInteger(feedbackId)) {
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

	ctrl.save = function(feedbackItem, submit) {
		ctrl.resetError();

		if (!feedbackItem || !feedbackItem.id || !feedbackItem.questions) {

			return;
		}
		Feedback.updateFeedback(feedbackItem.id, feedbackItem.questions, feedbackItem.shareFeedback, !!submit).then(function(response) {
			if (!!submit) {
				// update the summary view and return to the list view
				Model.getPendingFeedbackActions(true).then(function() {
					ctrl.navigateToList();
				});
			} else {
				ctrl.message = "Saved feedback."
			}
		}, function() {
			ctrl.error = "Could not save feedback.  Please try again later."
		});
	};

	ctrl.initialiseController();

	ctrl.navigateToList = function() {
		$location.path("worklist");
	};

	ctrl.resetError = function() {
		ctrl.error = undefined;
		ctrl.message = undefined;
	};
}]);