/*
 * Controller for submitting ad-hoc feedback
 */
fbControllers.controller('ProvideCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

    ctrl.candidateList = [];
    ctrl.feedbackRecipient = undefined;
    ctrl.message = undefined;
    ctrl.publishToRecipient = false;
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
    ctrl.error = undefined;
    Model.createAdHocFeedback(recipientEmail, message, publishToRecipient).then(function(response) {
      ctrl.submittedAdHocFeedback = response;
      ctrl.feedbackRecipient = undefined;
      ctrl.message = undefined;
      ctrl.publishToRecipient = false;
    }, function() {
      ctrl.error = "Could not create feedback.";
    });
  };

}]);