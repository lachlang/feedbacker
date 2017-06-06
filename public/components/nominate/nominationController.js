/*
 * Controller for feedback action
 */
fbControllers.controller('NominationCtrl',  ['$scope', '$log', 'Model', 'Nomination', 'Util', function($scope, $log, Model, Nomination, Util) {

  var ctrl = this;

	ctrl.nomineeCandidates = []
	ctrl.nominations = []
	ctrl.cycles = []
	ctrl.selectedCycle = undefined
	ctrl.nominee = undefined
	ctrl.message = undefined
	ctrl.success = undefined
	ctrl.update = undefined
	ctrl.error = undefined

	// get the pending actions
	Model.getActiveUsers().then(function(response) {
		ctrl.nomineeCandidates = response
	})

	Model.getCurrentNominations().then(function(response) {
		ctrl.nominations = response
	})

	Model.getActiveFeedbackCycles().then(function(response) {
		ctrl.cycles = response
		if (ctrl.cycles && ctrl.cycles.length >= 1) {
			ctrl.selectedCycle = ctrl.cycles[0]
		}
	})

  ctrl.addNomination = function(emailAddress, cycleId, personalMessage) {
    ctrl.resetMessages()

    // validate email
    if (!emailAddress || !Util.isValidEmail(emailAddress)) {
      ctrl.error = "Must send a nomination to a valid email address."
      return
    } else if (!cycleId) {
      ctrl.error = "You must select a feedback cycle for your nomination."
      return
    }
    ctrl.update = "Thank you.  Your feedback nomination is being created and a notification email is being sent to '" + emailAddress + "'."

    // update model
    Nomination.addNomination(emailAddress, cycleId, personalMessage).then(function() {
      ctrl.update = undefined
      ctrl.success = "Thank you. The feedback nomination has been created and an email notification has been sent to '" + emailAddress + "'."
      Model.getCurrentNominations(true).then(function(response) {
        ctrl.nominations = response
      })
    }, function() {
      ctrl.update = undefined
      ctrl.error = "Could not create nomination at this time.  Please try again later."
    })
  }

  ctrl.cancelNomination = function(nominationId) {
    ctrl.resetMessages()

    if (!nominationId) {
      ctrl.error = "No nomination selected to cancel."
      return
    }
    Nomination.cancelNomination(nominationId).then(function() {
      Model.getCurrentNominations(true).then(function(response) {
        ctrl.success = "Nomination successfully removed"
        ctrl.nominations = response
      })
    }, function() {
      ctrl.error = "Could not cancel nomination at this time.  Please try again later."
    })
  }

	ctrl.resetMessages = function() {
	  ctrl.success = undefined
	  ctrl.update = undefined
	  ctrl.error = undefined
	}
}])