/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model', '$location', 'Util', function($scope, $log, Model, $location, Util) {

	var ctrl = this;

	ctrl.error = undefined;
	ctrl.message = undefined;
	ctrl.feedback = {questions: []};

	ctrl.initialiseController = function() {
		var feedbackId = $location.search()["id"];

    if (feedbackId && Util.isInteger(feedbackId, true)) {
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
			ctrl.error = "Could not load feedback."
		}
	}

	ctrl.save = function(feedbackItem, submit) {
		ctrl.resetError();

		if (!feedbackItem || !feedbackItem.id || !feedbackItem.questions) {
			return;
		}
		Model.updateFeedbackDetail(feedbackItem.id, feedbackItem.questions, !!feedbackItem.shareFeedback, !!submit).then(function(response) {
			Model.getPendingFeedbackActions(true);
      ctrl.feedback = response

			if (!!submit) {
				// update the summary view and return to the list view
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
		$location.path("worklist");
	};

	ctrl.resetError = function() {
		ctrl.error = undefined;
		ctrl.message = undefined;
	};
}]);