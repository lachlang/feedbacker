/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('ReportsCtrl', ['$log', 'Model', 'Nomination', function($log, Model, Nomination) {

	var ctrl = this;

  ctrl.userReports = [];
  ctrl.cycleReports = [];
//  ctrl.displayFilter = 'current';

  Model.getUserReports().then(function(response) {
    ctrl.reports = response;
  });

  Model.getCycleReports().then(function(response) {
    ctrl.cycles = response;
  });

}]);
