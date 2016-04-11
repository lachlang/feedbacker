/*
 * Controller for feedback action
 */
fbControllers.controller('FeedbackActionCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

	ctrl.actions = [];

	// get the pending actions
	Model.getPendingFeedbackActions().then(function(response) {
		ctrl.actions = response;
	});

}]);