'use strict';

describe('reports controller [ReportsCtrl]', function() {

  var scope, deferredInit, deferred, reportsController, model, account;

  beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Account_) {

    scope = $rootScope.$new()

    deferred = $q.defer();
    deferredInit = $q.defer();

    account = _Account_
    spyOn(account, 'updateUser').and.returnValue(deferred.promise)

    model = _Model_;
    spyOn(model, 'getUserReports').and.returnValue(deferredInit.promise);
    spyOn(model, 'getCycleReports').and.returnValue(deferredInit.promise);

    reportsController = $controller('ReportsCtrl',{});
  }));

  describe('has valid initialisation values', function() {

    it('should define functions', function() {
      expect(angular.isFunction(reportsController.updateUser)).toBe(true);
    });

    it('for global controller variables', function() {
      expect(reportsController.userReports).toEqual([]);
      expect(reportsController.cycleReports).toEqual([]);
      expect(reportsController.displayFilter).toEqual('active');
    });

    it('and calls the necessary services to pre-populate the model', function(){
      expect(model.getUserReports).toHaveBeenCalled();
      expect(model.getCycleReports).toHaveBeenCalled();
    });
  });

  describe('', function() {

    it('should update the attributes of a user', inject(function($q) {
      var input = {"credentials":{"email":"email"}, "name":"name","role":"role","managerEmail":"manager@email.com","isAdmin":true,"isEnabled":false};
      reportsController.userReports = [input]
      reportsController.updateUser(input);
      expect(account.updateUser).toHaveBeenCalledWith(input.credentials.email,input.name,input.role,input.managerEmail, input.isAdmin, input.isEnabled);

      var result = { "data": { "body": {"name":"name", "role":"role", "managerEmail":"managerEmail", "isAdmin":true, credentials: {"email":"email", "status":"pants"}}}}
      deferred.resolve(result);
      scope.$digest();
      expect(reportsController.userReports).toEqual([{"name":"name", "role":"role", "managerEmail": "managerEmail", "isAdmin":true, "credentials": {"email":"email", "status":"pants"},"isEnabled": true}]);
    }));

    it('should update the attributes of a user and process an inactive user status', inject(function($q) {
      var input = {"credentials":{"email":"email"}, "name":"name","role":"role","managerEmail":"manager@email.com","isAdmin":true,"isEnabled":false};
      reportsController.userReports = [input]
      reportsController.updateUser(input);
      expect(account.updateUser).toHaveBeenCalledWith(input.credentials.email,input.name,input.role,input.managerEmail, input.isAdmin, input.isEnabled);

      var result = { "data": { "body": {"name":"name", "role":"role", "managerEmail":"managerEmail", "isAdmin":false, credentials: {"email":"email", "status":"Disabled"}}}}
      deferred.resolve(result);
      scope.$digest();
      expect(reportsController.userReports).toEqual([{"name":"name", "role":"role", "managerEmail": "managerEmail", "isAdmin":false, "credentials": {"email":"email", "status":"Disabled"},"isEnabled": false}]);
    }));
  })
});
