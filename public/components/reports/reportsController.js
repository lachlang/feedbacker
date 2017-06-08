/*
 * Controller search for connection, uploading bulk connections and for sending connection/invitation requests
 */
fbControllers.controller('ReportsCtrl', ['$log', 'Model', 'Account', 'Util', function($log, Model, Account, Util) {

	var ctrl = this;

  ctrl.userReports = [];
  ctrl.cycleReports = [];
  ctrl.displayFilter = 'active';

  Model.getUserReports().then(function(response) {
    console.log(response)
    ctrl.userReports = response.map(function(item) {
      if (item.person.credentials.status === 'Disabled') {
        item.person.isEnabled = false
      } else {
        item.person.isEnabled = true
      }
      return item;
    });
  });

  Model.getCycleReports().then(function(response) {
    ctrl.cycleReports = response;
  });

  ctrl.updateUser = function(user) {
    if (!Util.isValidEmail(user.managerEmail)) {
      ctrl.error = "Manager email must be a valid email format."
      return
    }
    Account.updateUser(user.credentials.email, user.name, user.role, user.managerEmail, user.isAdmin, user.isEnabled).then(function(result){
      $log.debug("[ReportCtrl.updateUser] Updated user...");
      $log.debug(result.data.body);
      var index = ctrl.userReports.indexOf(user);

      if (index !== -1) {
        ctrl.userReports[index] = result.data.body;

        if (ctrl.userReports[index].credentials.status === 'Disabled') {
          ctrl.userReports[index].isEnabled = false;
        } else {
          ctrl.userReports[index].isEnabled = true;
        }
      }
    })
  };

}]);
