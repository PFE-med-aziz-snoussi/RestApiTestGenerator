import { Component, OnInit, OnDestroy } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Subscription, forkJoin } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { LayoutService } from 'src/app/layout/service/app.layout.service';
import { ProjectService } from 'src/app/services/project.service';
import { StatisticsService } from 'src/app/services/statistics.service';
import { VersionService } from 'src/app/services/version.service';

@Component({
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],

})
export class DashboardComponent implements OnInit, OnDestroy {

  items!: MenuItem[];
  subscription!: Subscription;

  basicData: any;
  stackedData: any;
  lineData: any;
  pieData: any;
  radarData: any;
  polarAreaData: any;
  coverageData: any;
  comboData: any;

  chartOptions: any;
  stackedChartOptions: any;
  statistics: any = {};

  doughnutChartOptions: any;
  pieChartOptions: any;
  lineChartOptions: any;
  CoveredNbTotal: any = 0;

  constructor(private projectService: ProjectService, private versionService: VersionService, private statisticsService: StatisticsService, public layoutService: LayoutService) {
    this.subscription = this.layoutService.configUpdate$
      .pipe(debounceTime(25))
      .subscribe(() => {
        this.initChartOptions();
      });
  }

  ngOnInit() {
    this.initChartOptions();
    this.loadStatistics();
    this.loadVersionsAndExecutionData();

    this.items = [
      { label: 'Add New', icon: 'pi pi-fw pi-plus' },
      { label: 'Remove', icon: 'pi pi-fw pi-minus' }
    ];
  }

  initChartOptions() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.chartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }
      }
    };

    this.stackedChartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          stacked: true,
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          stacked: true,
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }
      }
    };

    this.doughnutChartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        },
        datalabels: {
          display: (context) => context.dataset.data.length === 1,
          align: 'center',
          anchor: 'center',
          formatter: (value, context) => {
            let sum = 0;
            const dataArr = context.chart.data.datasets[0].data;
            dataArr.map(data => {
              sum += data;
            });
            return sum;
          },
          color: textColor,
          font: {
            weight: 'bold',
            size: 20,
          }
        },
        title: {
          display: true,
          text: 'Coverage',
          color: textColor,
          font: {
            size: 16
          }
        }
      },
      cutout: '40%', 
    };

    this.pieChartOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        },
        datalabels: {
          formatter: (value, context) => {
            let sum = 0;
            const dataArr = context.chart.data.datasets[0].data;
            dataArr.map(data => {
              sum += data;
            });
            return sum;
          },
          color: textColor,
          font: {
            weight: 'bold',
            size: 20,
          }
        },
        title: {
          display: true,
          text: 'Tests',
          color: textColor,
          font: {
            size: 16
          }
        }
      },
      cutout: '40%',
    };

    this.lineChartOptions = {
      responsive: true,
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        },
        tooltip: {
          mode: 'index',
          intersect: false,
        },
        title: {
          display: true,
          text: 'Versions and Executions per Month',
          color: textColor,
          font: {
            size: 16
          }
        }
      },
      interaction: {
        mode: 'index',
        intersect: false,
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }
      },
      elements: {
        point: {
          radius: 5,
          hoverRadius: 7,
        },
        line: {
          tension: 0.4
        }
      }
    };
  }

  loadStatistics() {
    this.statisticsService.getStatistics().subscribe(data => {
      this.statistics = data;
    });
  }

  loadVersionsAndExecutionData() {
    this.versionService.getAllVersions().subscribe(versions => {
      const executionRequests = [];

      versions.forEach(version => {
        version.executions.forEach(execution => {
          executionRequests.push(this.projectService.getResultPostmanCollectionByexecution(execution.id));
        });
      });

      forkJoin(executionRequests).subscribe(responses => {
        const allStats = responses.map(response => this.readResponseAsJson(response));

        Promise.all(allStats).then(statsObjects => {
          const combinedStats = this.combineStats(statsObjects, versions);

          const processedData = this.processStatsData(combinedStats);

          this.basicData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Total Assertions',
                data: processedData.totalAssertions,
                backgroundColor: '#42A5F5'
              },
              {
                label: 'Total Failed Assertions',
                data: processedData.totalFailedAssertions,
                backgroundColor: '#FF6384'
              }
            ]
          };

          this.stackedData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Requests Total',
                data: processedData.requestsTotal,
                backgroundColor: '#66BB6A',
                stack: 'a'
              },
              {
                label: 'Requests Failed Total',
                data: processedData.requestsFailedTotal,
                backgroundColor: '#FFA726',
                stack: 'a'
              }
            ]
          };

          this.lineData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Versions per Month',
                data: processedData.versionsPerMonth,
                fill: false,
                borderColor: '#42A5F5',
                pointBackgroundColor: '#42A5F5',
                tension: 0.4
              },
              {
                label: 'Executions per Month',
                data: processedData.executionsPerMonth,
                fill: false,
                borderColor: '#FF6384',
                pointBackgroundColor: '#FF6384',
                tension: 0.4
              }
            ]
          };

          this.pieData = {
            labels: ['Passed Tests', 'Failed Tests'],
            datasets: [
              {
                data: [
                  combinedStats.totalAssertions - combinedStats.totalFailedAssertions,
                  combinedStats.totalFailedAssertions
                ],
                backgroundColor: ['#66BB6A', '#FF6384']
              }
            ]
          };

          this.radarData = {
            labels: processedData.labels,
            datasets: [
              {
                label: 'Response Time',
                data: processedData.responseTimes,
                borderColor: '#42A5F5',
                backgroundColor: 'rgba(66,165,245,0.2)'
              }
            ]
          };

          this.polarAreaData = {
            labels: ['Total Assertions', 'Total Failed Assertions', 'Requests Total', 'Requests Failed Total'],
            datasets: [
              {
                data: [
                  combinedStats.totalAssertions,
                  combinedStats.totalFailedAssertions,
                  combinedStats.requestsTotal,
                  combinedStats.requestsFailedTotal
                ],
                backgroundColor: ['#42A5F5', '#FF6384', '#66BB6A', '#FFA726']
              }
            ]
          };

          this.coverageData = {
            labels: ['Covered Requests', 'Not Covered Requests'],
            datasets: [
              {
                data: [processedData.coveredRequests, processedData.notCoveredRequests],
                backgroundColor: ['#66BB6A', '#FF6384']
              }
            ]
          };
console.log(processedData.coveredRequests)
          this.comboData = {
            labels: processedData.labels,
            datasets: [
              {
                type: 'line',
                label: 'Response Time',
                data: processedData.responseTimes,
                borderColor: '#42A5F5',
                backgroundColor: 'rgba(66,165,245,0.2)',
                fill: false
              },
              {
                type: 'bar',
                label: 'Requests Total',
                data: processedData.requestsTotal,
                backgroundColor: '#66BB6A'
              },
              {
                type: 'bar',
                label: 'Requests Failed Total',
                data: processedData.requestsFailedTotal,
                backgroundColor: '#FFA726'
              }
            ]
          };
        });
      });
    });
  }

  calculatePassRequests(assertions: any[]): boolean {
    if (!assertions || assertions.length === 0) {
      return false;
    }
    const passedAssertions = assertions.filter(assertion => !assertion.error).length;
    return passedAssertions === assertions.length;
  }

  readResponseAsJson(response: any): Promise<any> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const jsonObject = JSON.parse(reader.result as string);
          resolve(jsonObject);
        } catch (error) {
          reject(error);
        }
      };
      reader.onerror = () => reject(reader.error);
      const blob = new Blob([response], { type: 'application/json' });
      reader.readAsText(blob);
    });
  }

  combineStats(statsObjects: any[], versions: any[]): any {
    const combinedStats = {
      totalAssertions: 0,
      totalFailedAssertions: 0,
      requestsTotal: 0,
      requestsFailedTotal: 0,
      responseTimes: [],
      responseSizes: [],
      monthlyAssertions: {},
      monthlyFailedAssertions: {},
      coveredRequests: 0,
      notCoveredRequests: 0,
      monthlyVersions: {},
      monthlyExecutions: {}
    };

    statsObjects.forEach((stats, index) => {
      const version = versions[index];
      if (version && version.createdAt) {
        const month = this.convertTimestampToMonth(new Date(version.createdAt).getTime());

        if (!combinedStats.monthlyVersions[month]) {
          combinedStats.monthlyVersions[month] = 0;
          combinedStats.monthlyExecutions[month] = 0;
        }

        combinedStats.monthlyVersions[month] += 1;
        combinedStats.monthlyExecutions[month] += stats.run.executions.length;

        if (!combinedStats.monthlyAssertions[month]) {
          combinedStats.monthlyAssertions[month] = 0;
          combinedStats.monthlyFailedAssertions[month] = 0;
        }

        combinedStats.monthlyAssertions[month] += stats.run.stats.assertions.total;
        combinedStats.monthlyFailedAssertions[month] += stats.run.stats.assertions.failed;

        combinedStats.totalAssertions += stats.run.stats.assertions.total;
        combinedStats.totalFailedAssertions += stats.run.stats.assertions.failed;
        combinedStats.requestsTotal += stats.run.stats.requests.total;
        combinedStats.requestsFailedTotal += stats.run.stats.requests.failed;

        stats.run.executions.forEach((execution: any) => {
          combinedStats.responseTimes.push(execution.timings?.responseTime || 0);
          combinedStats.responseSizes.push(execution.timings?.responseSize || 0);

          if (this.calculatePassRequests(execution.assertions)) {
            combinedStats.coveredRequests++;
          } else {
            combinedStats.notCoveredRequests++;
          }
        });
      }
    });

    return combinedStats;
  }

  convertTimestampToMonth(timestamp: number): string {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', { month: 'short' });
  }

  processStatsData(combinedStats: any): any {
    const currentDate = new Date();
    const labels = [];

    for (let i = 6; i >= 0; i--) {
      const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1);
      labels.push(this.convertTimestampToMonth(date.getTime()));
    }

    const versionsPerMonth = labels.map(month => combinedStats.monthlyVersions[month] || 0);
    const executionsPerMonth = labels.map(month => combinedStats.monthlyExecutions[month] || 0);
    this.CoveredNbTotal += (combinedStats.coveredRequests+combinedStats.notCoveredRequests);

    return {
      labels,
      versionsPerMonth,
      executionsPerMonth,
      totalAssertions: labels.map(month => combinedStats.monthlyAssertions[month] || 0),
      totalFailedAssertions: labels.map(month => combinedStats.monthlyFailedAssertions[month] || 0),
      requestsTotal: labels.map(month => combinedStats.requestsTotal),
      requestsFailedTotal: labels.map(month => combinedStats.requestsFailedTotal),
      responseTimes: combinedStats.responseTimes,
      responseSizes: combinedStats.responseSizes,
      coveredRequests: combinedStats.coveredRequests,
      notCoveredRequests: combinedStats.notCoveredRequests
    };
  }

  calculateCoveragePercentage(totalAssertions: number, totalFailedAssertions: number): number {
    const passedAssertions = totalAssertions - totalFailedAssertions;
    return (passedAssertions / totalAssertions) * 100;
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  convertTimestampToDate(timestamp: number): string {
    const date = new Date(timestamp);
    const options: Intl.DateTimeFormatOptions = {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    };
    return date.toLocaleDateString('en-US', options) + ' ' + date.toLocaleTimeString('en-US', { hour12: false });
  }
}
