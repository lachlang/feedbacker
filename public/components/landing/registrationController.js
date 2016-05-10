/*
 * Controller for feedback action
 */
fbControllers.controller('RegistrationCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

	var ctrl = this;

	var register = function() {
		// validate 
		Account.register(ctrl.name, ctrl.email, ctrl.managerEmail, ctrl.password).then(function(response) {
			// success
		}, function(response) {
			// failure
		});
	};

	var forgotPassword = function() {

	};

}]);