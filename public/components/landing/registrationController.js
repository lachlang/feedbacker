/*
 * Controller for feedback action
 */
fbControllers.controller('RegistrationCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

	var ctrl = this;

	ctrl.register = function() {
		Account.register(ctrl.name, ctrl.role, ctrl.email, ctrl.password, ctrl.managerEmail).then(function(response) {
			$log.info("great success")
			$log.info(response)
		}, function(response) {
			$log.error("it failed")
			$log.error(response)
		});
	};

	ctrl.forgotPassword = function() {
		$log.info("forgotPassword");
		Account.register("Lockers", "Boss", "a@b.c", "asdfasdf", "b@b.c").then(function(response) {
			$log.info("great success")
			$log.info(response)
		}, function(response) {
			$log.error("it failed")
			$log.error(response)
		});
	};

}]);