import * as fs from 'fs'
import { ExportToCsv } from 'export-to-csv'

const gamesDir = process.argv[2]
const csvFilename = `${gamesDir}/games.csv`
const csvExporter = new ExportToCsv({
  fieldSeparator: ',',
  quoteStrings: '"',
  showLabels: true,
  useKeysAsHeaders: true,
});

const lineRegex = /\[([^"]+) "(.+)"\]/
const allGameData = []
fs.readdirSync(gamesDir).sort().forEach(filename => {
  const lines = fs.readFileSync(`${gamesDir}/${filename}`).toString().split('\n')
  const gameData = {}
  lines.forEach(line => {
    if (line.startsWith('1.')) {
      gameData['Moves'] = line.replace(/\r$/, '')
    } else {
      const match = line.match(lineRegex)
      if (match) {
        const [name, value] = match.slice(1, 3)
        gameData[name] = value
      }
    }
  })
  allGameData.push(gameData)
})
const csvData = csvExporter.generateCsv(allGameData, true)
fs.writeFileSync(csvFilename, csvData)
