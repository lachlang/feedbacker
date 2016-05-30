/*
 * Controller for feedback action
 */
fbControllers.controller('NominationCtrl',  ['$scope', '$log', 'Model', 'Nomination', function($scope, $log, Model, Nomination) {

	var ctrl = this;

	ctrl.nomineeCandidates = [];
	ctrl.nominations = [];
	ctrl.nominee = undefined;
	ctrl.error = undefined;

	// get the pending actions
	Model.getNomineeCandidates().then(function(response) {
		ctrl.nomineeCandidates = response;
	});

	Model.getCurrentNominations().then(function(response) {
		ctrl.nominations = response;
	});

	var validateEmail = function(word) {
		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		return re.test(word);
	}

	ctrl.addNomination = function(emailAddress) {
		ctrl.error = undefined;

		// validate email
		if (!emailAddress || !validateEmail(emailAddress)) {
			ctrl.error = "Must send a nomination to a valid email address.";
			return;
		}
		// update model
		Nomination.addNomination(emailAddress).then(function() {
			Model.getCurrentNominations(true).then(function(response) {
				ctrl.nominations = response;
			});
		}, function() {
			ctrl.error = "Could not create nomination at this time.  Please try again later.";
		});
	};

	ctrl.cancelNomination = function(nominationId) {
		ctrl.error = undefined;

		if (!nominationId) {
		 	ctrl.error = "No nomination selected to cancel.";
		 	return;
		}
		Nomination.cancelNomination(nominationId).then(function() {
			Model.getCurrentNominations(true).then(function(response) {
				ctrl.nominations = response;
			});
		}, function() {
			ctrl.error = "Could not cancel nomination at this time.  Please try again later.";
		});
	};
}]);