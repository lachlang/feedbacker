/*
 * Controller for feedback action
 */
fbControllers.controller('NominationCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

	ctrl.nomineeCandidates = [];
	ctrl.nominations = [];

	// get the pending actions
	Model.getNomineeCandidates().then(function(response) {
		ctrl.nomineeCandidates = response
	});

	Model.getCurrentNominations().then(function(response) {
		ctrl.nominations = response;
	});

	ctrl.addNomination = function(emailAddress) {

	};

	ctrl.cancelNomination = function(nominationId) {

	};

	ctrl.searchForNomination = function(searchTerm) {

	};

}]);