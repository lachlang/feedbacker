/*
 * Controller for feedback action
 */
fbControllers.controller('NominationCtrl',  ['$scope', '$log', 'Model', 'Nomination', function($scope, $log, Model, Nomination) {

	var ctrl = this;

	ctrl.nomineeCandidates = [];
	ctrl.nominations = [];
	ctrl.cycles = [];
	ctrl.selectedCycle = undefined;
	ctrl.nominee = undefined;
	ctrl.message = undefined;
	ctrl.error = undefined;

	// get the pending actions
	Model.getActiveUsers().then(function(response) {
		ctrl.nomineeCandidates = response;
	});

	Model.getCurrentNominations().then(function(response) {
		ctrl.nominations = response;
	});

	Model.getActiveFeedbackCycles().then(function(response) {
		ctrl.cycles = response;
		if (ctrl.cycles && ctrl.cycles.length >= 1) {
			ctrl.selectedCycle = ctrl.cycles[0];
		}
	});

	// TODO: pull this out into a utils service
	var validateEmail = function(word) {
		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		return re.test(word);
	}

	ctrl.addNomination = function(emailAddress, cycleId, personalMessage) {
		ctrl.error = undefined;

		// validate email
		if (!emailAddress || !validateEmail(emailAddress)) {
			ctrl.error = "Must send a nomination to a valid email address.";
			return;
		} else if (!cycleId) {
			ctrl.error = "You must select a feedback cycle for your nomination.";
			return;
		}
		// update model
		Nomination.addNomination(emailAddress, cycleId, personalMessage).then(function() {
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