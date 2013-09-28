from twisted.application import internet, service
from twisted.web import resource as twisted_resource, server

from bearloc.service import BearLocService
from bearloc import resource as bearloc_resource


application = service.Application('bearloc')
bearloc = BearLocService(db="bearloc.db", content=['report', 'localize'])
serviceCollection = service.IServiceCollection(application)
# HTTP service
site = server.Site(twisted_resource.IResource(bearloc))
internet.TCPServer(10080, site).setServiceParent(serviceCollection)
