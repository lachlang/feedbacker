/*
 * Controller for administrative operations
 */
fbControllers.controller('AdminCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;

  ctrl.reviewCycles = [];
  ctrl.selectedCycle = undefined;
  ctrl.selectedCycleDetails = undefined;
  ctrl.registeredUsers = [];
  ctrl.selectedUser = undefined;
  ctrl.error = undefined;

  ctrl.startPopup = {
    opened: false
  };

  ctrl.endPopup = {
    opened: false
  };

  Model.getAllFeedbackCycles().then(function(result) {
    ctrl.reviewCycles = result;
  }, function(result) {
    ctrl.error = "Could not retrieve review cycle list.";
  });

  Model.getRegisteredUsers().then(function(result) {
    ctrl.registeredUsers = result;
  });

  ctrl.clearSelectedCycle = function() {
    ctrl.selectedCycleDetails = undefined;
    ctrl.selectedCycle = undefined;
  };

  ctrl.clearSelectedUser = function() {
    ctrl.selectedUser = undefined;
  };

  ctrl.getFeedbackCycle = function(cycleId) {
    Model.getFeedbackCycle(cycleId).then(function(result) {
      ctrl.selectedCycleDetails = result;
    });
  };

  ctrl.initialiseNewCycle = function() {
    ctrl.selectedCycleDetails = {
            "active": false,
            "hasForcedSharing": false,
            "hasOptionalSharing": true,
            "isThreeSixtyReview": false,
            "questions":
            [ {"format": "RADIO"}, {"format": "RADIO"}, {"format": "RADIO"}, {"format": "RADIO"}, {"format": "RADIO"}]
            };
  };

  ctrl.updateFeedbackCycle = function() {
    alert("Please contact your administrator to implement this exciting feature.");
  };

  ctrl.createNewFeedbackCycle = function() {
    alert("Please contact your administrator to implement this exciting feature.");
  };

  ctrl.updateUser = function() {
    alert("Please contact your administrator to implement this exciting feature.");
  };

  ctrl.removeQuestion = function(index) {
    ctrl.selectedCycleDetails.questions.splice(index, 1);
  };

  ctrl.addQuestion = function() {
    if (ctrl.selectedCycleDetails && ctrl.selectedCycleDetails.questions) {
      ctrl.selectedCycleDetails.questions.push({ "format": "RADIO" });
    }
  };

  ctrl.openStart = function() {
    ctrl.startPopup.opened = true;
  };

  ctrl.openEnd = function() {
    ctrl.endPopup.opened = true;
  };
}]);