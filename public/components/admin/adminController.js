/*
 * Controller for administrative operations
 */
fbControllers.controller('AdminCtrl',  ['$scope', '$log', 'Model', 'Account', function($scope, $log, Model, Account) {

	var ctrl = this;

  ctrl.reviewCycles = [];
  ctrl.selectedCycle = undefined;
  ctrl.selectedCycleDetails = undefined;
  ctrl.registeredUsers = [];
  ctrl.selectedUser = undefined;
  ctrl.showNewCycleView = false;
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
    ctrl.showNewCycleView = false;
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
    ctrl.showNewCycleView = true;
    ctrl.selectedCycleDetails = {
            "active": false,
            "hasForcedSharing": false,
            "hasOptionalSharing": true,
            "isThreeSixtyReview": false,
            "questions":
            [ {"format": "RADIO"}, {"format": "RADIO"}, {"format": "RADIO"}, {"format": "RADIO"}, {"format": "RADIO"}]
            };
  };

  ctrl.updateFeedbackCycle = function(cycle) {
    alert("Please contact your administrator to implement this exciting feature.");
  };

  ctrl.createNewFeedbackCycle = function(cycle) {
    ctrl.showNewCycleView = false;
    alert("Please contact your administrator to implement this exciting feature.");
  };

  ctrl.updateUser = function(user) {
    ctrl.error = undefined;
    if (!user || !user.email || !user.name || !user.role || !user.managerEmail) {
      $log.error("here")
      $log.error(user)
      ctrl.error = "";
      return;
    }
    Account.updateUser(user.email, user.name, user.role, user.managerEmail, user.isAdmin, user.isEnabled).then(function(result){
      // TODO: This is a bug and will put a full person object in the summary person search array.
      // It will be fine for now.  LG 2017-05-18
      ctrl.selectedUser = result.data.body;
      ctrl.selectedUser.display = result.data.body.name + "(" + result.data.body.credentials.email + ")"
      ctrl.selectedUser.isDisabled = result.data.body.credentials.status == 'Disabled';
      ctrl.selectedUser.email = result.data.body.credentials.email;
      $log.debug("[AdminCtrl.updateUser] Updated user...");
      $log.debug(result.data.body);
    })
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