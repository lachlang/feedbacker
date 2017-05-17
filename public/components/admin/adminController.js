/*
 * Controller for administrative operations
 */
fbControllers.controller('AdminCtrl',  ['$scope', '$log', 'Model', function($scope, $log, Model) {

	var ctrl = this;


  ctrl.reviewCycles = [];
  ctrl.selectedCycle = undefined;
  ctrl.registeredUsers = [];
  ctrl.error = undefined;

  Model.getAllFeedbackCycles().then(function(result) {
    ctrl.reviewCycles = result;
  }, function(result) {
    ctrl.error = "Could not retrieve review cycle list.";
  });

  Model.getRegisteredUsers().then(function(result) {
    ctrl.registeredUsers = result;
  });

  ctrl.clearSelectedCycle = function() {
    ctrl.selectedCycle = undefined;
  };

  ctrl.createNewCycle = function() {
  };

  ctrl.updateFeedbackCycle = function() {
  };

  ctrl.createFeedbackCycle = function() {
  };
}]);