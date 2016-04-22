/*
 * Controller for feedback action
 */
fbControllers.controller('EditCtrl',  ['$scope', '$log', 'Model', 'uibButtonConfig', '$location', function($scope, $log, Model, btnConfig, $location) {

	btnConfig.activeClass = 'btn-primary';

	var ctrl = this;
	var feedbackId = undefined;

	ctrl.questions = [];

	ctrl.initialiseController = function() {
		var feedbackId = $location.search()['id'];
		if (feedbackId) {
			Model.getFeedbackDetail(feedbackId).then(function(response) {
				ctrl.question = response;
			});
		} else {	
			Model.getQuestionSet().then(function(response) {
				ctrl.questions = response;
			});
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