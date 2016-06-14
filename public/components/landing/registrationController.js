/*
 * Controller for feedback action
 */
fbControllers.controller('RegistrationCtrl',  ['$rootScope', '$log', '$location', 'Account', function($rootScope, $log, $location, Account) {

	var ctrl = this;
	ctrl.error = undefined;
	ctrl.message = undefined;

	ctrl.register = function() {
		ctrl.message = undefined;
		ctrl.error = undefined;

		Account.register(ctrl.name, ctrl.role, ctrl.email, ctrl.password, ctrl.managerEmail).then(function(response) {
			ctrl.message = "Thankyou for registering.  An activation email has been sent to your email address."
			ctrl.name = undefined
			ctrl.role = undefined
			ctrl.email = undefined
			ctrl.password = undefined
			ctrl.managerEmail = undefined
		}, function(response) {
			ctrl.error = "We could not register you at this time."
			$log.error("it failed")
			$log.error(response)
		});
	};

	$rootScope.$on('inactive-account', function() {
		$log.debug("Account is inactive");
		ctrl.error = "You account is not yet active.  Please follow the link sent in your activation email.";
	});

	$rootScope.$on('invalid-credentials', function() {
		$log.debug("Invalid username/password");
		ctrl.error = "Your username and password were invalid.";
	});

}]);