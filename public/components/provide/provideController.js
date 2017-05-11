/*
 * Controller for submitting ad-hoc feedback
 */
fbControllers.controller('ProvideCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

    ctrl.candidateList = [];
    ctrl.feedbackCandidate = undefined;
    ctrl.message = undefined;
    ctrl.publishToCandidate = false;
    ctrl.error = undefined;
    ctrl.submittedAdHocFeedback = [];

	// get the registered users
	Model.getNomineeCandidates().then(function(response) {
		ctrl.candidateList = response;
	});

	// get the users ad-hoc feedback history
	Model.getSubmittedAdHocFeedback().then(function(response) {
		ctrl.submittedAdHocFeedback = response;
	});

  ctrl.submitAdHocFeedback = function(recipientEmail, message, publishToRecipient) {
		console.log(recipientEmail)
		console.log(message)
		console.log(publishToRecipient)
    Model.createAdHocFeedback(recipientEmail, message, publishToRecipient).then(function(response) {
      ctrl.submittedAdHocFeedback = response;
    });
  };

}]);