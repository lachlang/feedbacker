/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('ReportsCtrl', ['$log', 'Model', 'Nomination', function($log, Model, Nomination) {

	var ctrl = this;

  ctrl.userReports = [];
  ctrl.cycleReports = [];
  ctrl.displayFilter = 'active';

  Model.getUserReports().then(function(response) {
    ctrl.userReports = response;
  });

  Model.getCycleReports().then(function(response) {
    ctrl.cycleReports = response;
  });

  ctrl.updateUser = function(user){
  
  }
}]);
