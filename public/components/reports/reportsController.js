/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('ReportsCtrl', ['$log', 'Model', 'Nomination', 'Util', function($log, Model, Nomination, Util) {

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

  ctrl.updateUser = function(user) {
    if (!Util.isValidEmail(user.managerEmail)) {
      ctrl.error = "Manager email must be a valid email format."
      return
    }
    Account.updateUser(user.email, user.name, user.role, user.managerEmail, user.isAdmin, user.isEnabled).then(function(result){
      if (!ctrl.selectedUser) {
        ctrl.selectedUser = {};
      }
      ctrl.selectedUser.display = result.data.body.name + " (" + result.data.body.credentials.email + ")";
      ctrl.selectedUser.name = result.data.body.name;
      ctrl.selectedUser.role = result.data.body.role;
      ctrl.selectedUser.managerEmail = result.data.body.managerEmail;
      ctrl.selectedUser.isEnabled = (result.data.body.credentials.status != 'Disabled');
      ctrl.selectedUser.isAdmin = result.data.body.isAdmin;
      $log.debug("[AdminCtrl.updateUser] Updated user...");
      $log.debug(ctrl.selectedUser);
    })
  };

}]);
