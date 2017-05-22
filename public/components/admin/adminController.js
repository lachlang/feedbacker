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
  ctrl.flattenedQuestionResponse = {};
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
    ctrl.flattenedQuestionResponse = {};
  };

  ctrl.clearSelectedUser = function() {
    ctrl.selectedUser = undefined;
  };

  ctrl.getFeedbackCycle = function(cycleId) {
    Model.getFeedbackCycle(cycleId).then(function(result) {
      ctrl.setSelectedCycleDetails(result);
      ctrl.initialiseQuestionResponse(result.questions);
    });
  };

  ctrl.initialiseQuestionResponse = function(questions) {
    ctrl.flattenedQuestionResponse = {};
    questions.forEach(function(element, index) {
      return ctrl.flattenedQuestionResponse[index] = element.responseOptions.join('\n');
    });
  }

  ctrl.initialiseNewCycle = function() {
    ctrl.selectedCycleDetails = {
            "active": false,
            "hasForcedSharing": false,
            "hasOptionalSharing": true,
            "isThreeSixtyReview": false,
            "questions":
            [ {"responseOptions":[],"format": "RADIO"},
              {"responseOptions":[],"format": "RADIO"},
              {"responseOptions":[],"format": "RADIO"},
              {"responseOptions":[],"format": "RADIO"},
              {"responseOptions":[],"format": "RADIO"}]
            };
    ctrl.flattenedQuestionResponse = {"0":[],"1":[],"2":[],"3":[],"4":[]}
  };

  ctrl.saveChanges = function(cycle, createNew) {
    ctrl.error = undefined;
    if (createNew) {
      ctrl.createNewFeedbackCycle(cycle);
    } else {
      ctrl.updateFeedbackCycle(cycle);
    }
  };

  ctrl.updateFeedbackCycle = function(cycle) {
    Model.updateFeedbackCycle(cycle).then(function(result) {
      ctrl.setSelectedCycleDetails(result)
      ctrl.selectedCycle.label = result.label;
      ctrl.selectedCycle.startDate = result.startDate;
      ctrl.selectedCycle.endDate = result.endDate;
      ctrl.selectedCycle.active = result.active;
      ctrl.selectedCycle.hasForcedSharing = result.hasForcedSharing;
      ctrl.selectedCycle.hasOptionalSharing = result.hasOptionalSharing;
      ctrl.initialiseQuestionResponse(result.questions)
    });
  };

  ctrl.createNewFeedbackCycle = function(cycle) {
    Model.createFeedbackCycle(cycle).then(function(result) {
      ctrl.setSelectedCycleDetails(result)
      ctrl.reviewCycles.push(result);
      ctrl.selectedCycle = result;
    });
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
      ctrl.selectedUser.display = result.data.body.name + "(" + result.data.body.credentials.email + ")";
      ctrl.selectedUser.role = result.data.body.role;
      ctrl.selectedUser.name = result.data.body.name;
      ctrl.selectedUser.isDisabled = (result.data.body.credentials.status === 'Disabled');
      ctrl.selectedUser.isAdmin = result.data.body.isAdmin;
      $log.debug("[AdminCtrl.updateUser] Updated user...");
      $log.debug(result.data.body);
      $log.debug(ctrl.selectedUser);
    })
  };

  ctrl.removeQuestion = function(index) {
    ctrl.selectedCycleDetails.questions.splice(index, 1);
  };

  ctrl.addQuestion = function() {
    if (ctrl.selectedCycleDetails && ctrl.selectedCycleDetails.questions) {
      ctrl.selectedCycleDetails.questions.push({ "responseOptions":[], "format": "RADIO" });
      ctrl.flattenedQuestionResponse[ctrl.selectedCycleDetails.questions.length] = [];
    }
  };

  ctrl.openStart = function() {
    ctrl.startPopup.opened = true;
  };

  ctrl.openEnd = function() {
    ctrl.endPopup.opened = true;
  };

  ctrl.setSelectedCycleDetails = function(cycle) {
    ctrl.selectedCycleDetails = cycle;
    ctrl.selectedCycleDetails.startDate = new Date(ctrl.selectedCycleDetails.startDate);
    ctrl.selectedCycleDetails.endDate = new Date(ctrl.selectedCycleDetails.endDate);
  };

  ctrl.updateQuestionResponse = function(question, responses) {
    question.responseOptions = responses.trim().split('\n');
  };
}]);