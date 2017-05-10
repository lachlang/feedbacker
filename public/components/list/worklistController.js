/*
 * Controller for managing the worklist
 */
fbControllers.controller('WorklistCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

	ctrl.user = {};
	ctrl.pendingActions = [];

	// get the pending actions
	Model.getCurrentUser().then(function(response) {
		ctrl.user = response;
	});

	Model.getPendingFeedbackActions().then(function(response) {
		ctrl.pendingActions = response;
	});

}]);